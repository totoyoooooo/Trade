const API_KEY = '507584ffde4ed3d97c84a18a763c33be';

/**
 * 逆地理编码：根据经纬度获取地址
 */
export function getAddress(query) {
    const { longitude, latitude } = query;
    const location = `${longitude},${latitude}`;
    const url = `https://restapi.amap.com/v3/geocode/regeo?output=json&location=${location}&key=${API_KEY}`;

    return fetch(url)
        .then(res => res.json())
        .then(data => {
            if (data.status == 1) {
                return { data: data.regeocode, code: 200 };
            } else {
                return { data: null, code: 500, msg: data.info };
            }
        })
        .catch(() => ({ data: null, code: 500, msg: '请求失败' }));
}